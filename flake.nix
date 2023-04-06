{
  description = "github:bitprophet/switchboard";

  inputs = {
    # TODO: ensure my system level flake knows how to override this (and then
    # maybe just don't set it in here lol? still wants some kinda easily
    # controllable systemwide default...)
    # A specific pin of nixos-22.05 branch from shortly before 22.11 came out.
    nixpkgs.url = "github:NixOS/nixpkgs/3933d8bb9120573c0d8d49dc5e890cb211681490";
  };

  outputs = { self, nixpkgs }: let
    system = "x86-64-linux";
    name = "switchboard";
    lein = nixpkgs.${system}.pkgs.leiningen;
  in {
    # Extra checks, on top of the default (which is to evaluate all inputs and packages.*)
    # TODO: is there any 'default' check, or a convention for "this is my
    # single check for this one flake"? besides what I'm doing now, which is
    # the name or packageName variable convention.
    checks.${system}.${name} = {};

    # Default package to build (unclear how this interplays with
    # defaultPackage.$system = xxx, exactly, though that at least seems to
    # _usually_ be a reference to a packages.$system attribute??)
    packages.${system}.default = stdenv.mkDerivation {
      pname = name;
      # TODO: how to get from project.clj?
      version = "0.1.0-SNAPSHOT";
      src = ./;
      buildInputs = [lein];
      builder = lein;
    };

    # TODO: devShell, if it can work for zsh

    # TODO: app(s)?
    # TODO: default 'nix run' will run the default package's named attribute as
    # a binary - so eg packages.$system.switchboard = xxx, would attempt to run
    # `switchboard` - not useful here but quite possibly for other flakes of
    # mine?
    # TODO: OTOH, if you think of the app/run feature as analogue to make or
    # invoke, a blanket default is uncommon, usually the 'default invocation'
    # would go unused or be equivalent to 'nix build' or 'nix check'?
  };
}
